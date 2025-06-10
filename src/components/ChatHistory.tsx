import { CHAT_HISTORY } from "@/config";
import { useAppSelector } from "@/redux/store";
import type { ChatMessage } from "@/types";
import { cn } from "@/utils";
import { useEffect, useRef, useState } from "react";

export interface ChatHistoryProps extends React.ComponentProps<"div"> {
  chatId: string;
}

export function ChatHistory({ chatId, className, ...props }: ChatHistoryProps) {
  const { token } = useAppSelector((state) => state.auth);

  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [chatMessages, setChatMessages] = useState<ChatMessage[] | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    const fetchChats = async () => {
      abortControllerRef.current?.abort();
      abortControllerRef.current = new AbortController();

      setIsLoading(true);

      try {
        const response = await fetch(`${CHAT_HISTORY}/${chatId}`, {
          signal: abortControllerRef.current?.signal,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        const chatMessages = (await response.json()) as ChatMessage[];
        setChatMessages(chatMessages);
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
      <section className={cn("w-full flex-col justify-end h-full", className)}>
        {chatMessages &&
          chatMessages.map((msg) => (
            <span key={JSON.stringify(msg)}>{msg.content}</span>
          ))}
      </section>
    </>
  );
}
