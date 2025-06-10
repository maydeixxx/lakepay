import { CHAT_HISTORY } from "@/config";
import { useAppSelector } from "@/redux/store";
import type { ChatMessage } from "@/types";
import { cn } from "@/utils";
import { useEffect, useRef, useState } from "react";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { Card } from "./Card";
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
import { useChatRoom } from "@/hooks/useChatRoom";

export interface ChatHistoryProps extends React.ComponentProps<"div"> {
  chatId: string;
}

export function ChatHistory({ chatId, className, ...props }: ChatHistoryProps) {
  const { user } = useAppSelector((state) => state.auth);
  const { history, info, pushMessage } = useChatRoom(chatId);

  useSubscription("/user/queue/messages", (message) =>
    pushMessage(JSON.parse(message.body))
  );

  return (
    <>
      <section className={cn("w-full h-full flex flex-col gap-4", className)}>
        {history &&
          history.map((msg) => (
            <Card
              className={cn(
                "bg-on-card-dark flex flex-col gap-2 p-4 w-fit",
                msg.sender.id == user?.id ? "ml-auto" : ""
              )}
              key={JSON.stringify(msg)}
            >
              <div
                className={cn(
                  "flex flex-row items-center gap-4",
                  msg.sender.id == user?.id ? "flex-row-reverse" : ""
                )}
              >
                <img
                  src={msg.sender.urlPhoto}
                  alt=""
                  className="rounded-full size-12"
                />
                <span>{msg.sender.username}</span>
              </div>
              <span>{msg.content}</span>
            </Card>
          ))}
      </section>
    </>
  );
}
