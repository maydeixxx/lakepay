import { Card } from "@/components/Card";
import { Input } from "@/components/Input";
import { ProductView } from "@/components/ProductView";
import { ADS_CATEGORY_URL, categories, CHAT_LIST, WS_URL } from "@/config";
import type { ChatRoom, Product } from "@/types";
import { photoFromCategory } from "@/utils";
import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate, useParams } from "react-router";
import game from "@/assets/cs2.webp";
import { ChatHistory } from "@/components/ChatHistory";
import { useAppSelector } from "@/redux/store";
import { ChatView } from "@/components/ChatView";
import { StompSessionProvider } from "react-stomp-hooks";

export default function ChatsPage() {
  const { chatId } = useParams<{ chatId: string }>();
  const { token } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();

  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [chatRooms, setChatRooms] = useState<ChatRoom[] | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    if (!token) {
      navigate("/login");
    }

    const fetchChats = async () => {
      abortControllerRef.current?.abort();
      abortControllerRef.current = new AbortController();

      setIsLoading(true);

      try {
        const response = await fetch(CHAT_LIST, {
          signal: abortControllerRef.current?.signal,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        const chatRooms = (await response.json()) as ChatRoom[];
        setChatRooms(chatRooms);
      } catch (e: any) {
        if (e.name === "AbortError") {
          console.log("Aborted");
          return;
        }

        setError(e);
      } finally {
        setIsLoading(false);
      }
    };

    fetchChats();
  }, []);

  return (
    <>
      <StompSessionProvider
        url={WS_URL}
        connectHeaders={{
          Authorization: `Bearer ${token}`
        }}
      >
        <section className="container mx-auto py-16 flex gap-8 min-h-[calc(100vh-10rem)]">
          <aside className="bg-card-dark basis-1/4 p-4 rounded-xl flex flex-col gap-4">
            <Input
              placeholder="Найти чат..."
              className="bg-on-card-dark placeholder:text-on-card-foreground text-on-card-dark-foreground min-h-12 mb-4"
            />
            {chatRooms &&
              chatRooms.map((room) => (
                <NavLink to={`/chat/${room.id}`} key={room.id}>
                  <Card className="bg-on-card-dark text-on-card-dark-foreground flex flex-row gap-8 p-4 items-center">
                    <img
                      src={room.sender.urlPhoto}
                      alt=""
                      className="rounded-full size-12"
                    />
                    <span>{room.sender.username}</span>
                  </Card>
                </NavLink>
              ))}

            <NavLink to={`/chat/${1}`}>
              <Card className="bg-on-card-dark text-on-card-dark-foreground flex flex-row gap-8 p-4 items-center">
                <img src={game} alt="" className="rounded-full size-12" />
                <span>Username</span>
              </Card>
            </NavLink>
          </aside>
          <main className="basis-3/4 bg-card-dark rounded-xl p-4">
            {chatId && <ChatView chatId={chatId}></ChatView>}
          </main>
        </section>
      </StompSessionProvider>
    </>
  );
}
