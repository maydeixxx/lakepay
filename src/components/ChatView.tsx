import { CHAT_INFO } from "@/config";
import { useAppSelector } from "@/redux/store";
import type { ChatRoom } from "@/types";
import { useEffect, useRef, useState } from "react";
import { Card } from "./Card";
import { ChatHistory } from "./ChatHistory";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from "./Form";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Input } from "./Input";
import { Button } from "./Button";

export interface ChatViewProps extends React.ComponentProps<"div"> {
  chatId: string;
}

const formSchema = z.object({
  message: z.string({ required_error: "" })
});

export function ChatView({ chatId, className, ...props }: ChatViewProps) {
  const { token } = useAppSelector((state) => state.auth);

  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [chatRoom, setChatRoom] = useState<ChatRoom | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    const fetchChats = async () => {
      abortControllerRef.current?.abort();
      abortControllerRef.current = new AbortController();

      setIsLoading(true);

      try {
        const response = await fetch(`${CHAT_INFO}/${chatId}`, {
          signal: abortControllerRef.current?.signal,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        const chatRoom = (await response.json()) as ChatRoom;
        setChatRoom(chatRoom);
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

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema)
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    console.log(values);
  }

  return (
    <>
      <section className="h-full flex flex-col gap-4">
        <Card className="w-full bg-on-card-dark p-4 flex-row items-center gap-4 text-on-card-dark-foreground">
          <img
            src={chatRoom?.recipient.urlPhoto}
            alt=""
            className="size-12 rounded-full"
          />
          <span>{chatRoom?.recipient.username}</span>
        </Card>
        <ChatHistory chatId={chatId} />
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="flex gap-4">
            <FormField
              control={form.control}
              name="message"
              render={({ field }) => (
                <FormItem className="w-full">
                  <FormControl>
                    <Input
                      placeholder="Сообщение..."
                      className="bg-on-card-dark placeholder:text-on-card-foreground text-on-card-dark-foreground min-h-12 mb-4"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button variant="secondary" type="submit">
              Отправить
            </Button>
          </form>
        </Form>
      </section>
    </>
  );
}
