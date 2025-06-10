import { addChatMessage, resetChatRoom } from "@/features/chat/chatRoomActions";
import {
  fetchChatRoomHistory,
  fetchChatRoomInfo
} from "@/features/chat/chatRoomThunkActions";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import type { ChatMessage } from "@/types";
import { useCallback, useEffect, useRef } from "react";

export function useChatRoom(chatId: string | undefined) {
  const dispatch = useAppDispatch();
  const abortInfoRef = useRef<AbortController | null>(null);
  const abortHistoryRef = useRef<AbortController | null>(null);

  const { info, history, infoStatus, historyStatus } = useAppSelector(
    (state) => state.chat
  );

  const refetchInfo = useCallback(() => {
    if (!chatId) return;
    abortInfoRef.current?.abort();
    abortInfoRef.current = new AbortController();
    dispatch(fetchChatRoomInfo(chatId, abortInfoRef.current));
  }, [chatId, dispatch]);

  const refetchHistory = useCallback(() => {
    if (!chatId) return;
    abortHistoryRef.current?.abort();
    abortHistoryRef.current = new AbortController();
    dispatch(fetchChatRoomHistory(chatId, abortHistoryRef.current));
  }, [chatId, dispatch]);

  const pushMessage = useCallback(
    (msg: ChatMessage) => dispatch(addChatMessage(msg)),
    [dispatch]
  );

  useEffect(() => {
    if (!chatId) return;

    dispatch(resetChatRoom());
    refetchInfo();
    refetchHistory();

    return () => {
      abortHistoryRef.current?.abort();
      abortInfoRef.current?.abort();
    };
  }, [chatId]);

  const isLoading = infoStatus === "loading" || historyStatus === "loading";

  return {
    info,
    history,
    infoStatus,
    historyStatus,
    isLoading,
    refetchInfo,
    refetchHistory,
    pushMessage
  };
}
