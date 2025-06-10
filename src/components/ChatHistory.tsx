import { CHAT_HISTORY } from "@/config";
import { useAppSelector } from "@/redux/store";
import type { ChatMessage } from "@/types";
import { cn } from "@/utils";
import { useEffect, useRef, useState } from "react";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { Card } from "./Card";
import { useChatRoom } from "@/hooks/useChatRoom";

export interface ChatHistoryProps extends React.ComponentProps<"div"> {
  chatId: string;
}

export function ChatHistory({ chatId, className, ...props }: ChatHistoryProps) {
  const { user } = useAppSelector((state) => state.auth);
  const { history, info, pushMessage } = useChatRoom(chatId);
  const lastMsgRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    lastMsgRef.current?.scrollIntoView({
      behavior: "smooth"
    });
  }, [history]);

  useSubscription("/user/queue/messages", (message) =>
    pushMessage(JSON.parse(message.body))
  );

  return (
    <>
      <section
        className={cn(
          "w-full h-full flex flex-col gap-4 overflow-y-auto",
          className
        )}
      >
        {history &&
          history.map((msg, i) => (
            <Card
              className={cn(
                "bg-on-card-dark flex flex-col gap-2 p-4 max-w-xl",
                msg.sender.id == user?.id ? "ml-auto" : "mr-auto"
              )}
              key={JSON.stringify(msg)}
              ref={i == history.length - 1 ? lastMsgRef : null}
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
              <span className="text-wrap break-words">{msg.content}</span>
            </Card>
          ))}
      </section>
    </>
  );
}
