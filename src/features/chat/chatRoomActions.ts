import type { ChatRoom, ChatMessage } from "@/types";

export type ChatRoomActionType =
  | "chatRoom/reset"
  | "chatRoom/loadingInfo"
  | "chatRoom/loadedInfoSuccess"
  | "chatRoom/loadedInfoFailure"
  | "chatRoom/loadingHistory"
  | "chatRoom/loadedHistorySuccess"
  | "chatRoom/loadedHistoryFailure"
  | "chatRoom/addMessage";

export type ChatRoomAction =
  | { type: "chatRoom/reset" }
  | { type: "chatRoom/loadingInfo" }
  | { type: "chatRoom/loadedInfoSuccess"; payload: ChatRoom }
  | { type: "chatRoom/loadedInfoFailure" }
  | { type: "chatRoom/loadingHistory" }
  | { type: "chatRoom/loadedHistorySuccess"; payload: ChatMessage[] }
  | { type: "chatRoom/loadedHistoryFailure" }
  | { type: "chatRoom/addMessage"; payload: ChatMessage };

export const resetChatRoom = (): ChatRoomAction => ({ type: "chatRoom/reset" });

export const loadingChatRoomInfo = (): ChatRoomAction => ({
  type: "chatRoom/loadingInfo"
});

export const loadedChatRoomInfoSuccess = (room: ChatRoom): ChatRoomAction => ({
  type: "chatRoom/loadedInfoSuccess",
  payload: room
});

export const loadedChatRoomInfoFailure = (): ChatRoomAction => ({
  type: "chatRoom/loadedInfoFailure"
});

export const loadingChatRoomHistory = (): ChatRoomAction => ({
  type: "chatRoom/loadingHistory"
});

export const loadedChatRoomHistorySuccess = (
  msgs: ChatMessage[]
): ChatRoomAction => ({
  type: "chatRoom/loadedHistorySuccess",
  payload: msgs
});

export const loadedChatRoomHistoryFailure = (): ChatRoomAction => ({
  type: "chatRoom/loadedHistoryFailure"
});

export const addChatMessage = (msg: ChatMessage): ChatRoomAction => ({
  type: "chatRoom/addMessage",
  payload: msg
});
