import { CHAT_INFO } from "@/config";
import { useAppSelector } from "@/redux/store";
import type { ChatMessage, ChatRoom } from "@/types";
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
import { useStompClient } from "react-stomp-hooks";
import { useChatRoom } from "@/hooks/useChatRoom";

export interface ChatViewProps extends React.ComponentProps<"div"> {
  chatId: string;
}

export function ChatView({ chatId, className, ...props }: ChatViewProps) {
  const stompClient = useStompClient();
  const { info, pushMessage } = useChatRoom(chatId);

  const formSchema = z.object({
    message: z.string({ required_error: "" })
  });

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema)
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    const newMessage = {
      sender: info?.sender,
      recipient: info?.recipient,
      content: values.message,
      date: new Date().toISOString()
    } as ChatMessage;
    console.log(newMessage);

    if (stompClient) {
      stompClient.publish({
        destination: "/chat/send",
        body: JSON.stringify(newMessage)
      });
      pushMessage(newMessage);
      form.setValue("message", "");
    }
  }

  return (
    <>
      <section className="h-full flex flex-col gap-4  max-h-[calc(100vh-16rem)]">
        <Card className="w-full bg-on-card-dark p-4 flex-row items-center gap-4 text-on-card-dark-foreground">
          <img
            src={info?.recipient.urlPhoto}
            alt=""
            className="size-12 rounded-full"
          />
          <span>{info?.recipient.username}</span>
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
