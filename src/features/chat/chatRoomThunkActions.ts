import { CHAT_INFO, CHAT_HISTORY } from "@/config";
import type { AppThunk } from "@/redux/store";
import type { ChatRoom, ChatMessage } from "@/types";
import {
  loadingChatRoomInfo,
  loadedChatRoomInfoSuccess,
  loadedChatRoomInfoFailure,
  loadingChatRoomHistory,
  loadedChatRoomHistorySuccess,
  loadedChatRoomHistoryFailure
} from "./chatRoomActions";

/**
 * Fetch chat room info with AbortController support
 */
export const fetchChatRoomInfo =
  (chatId: string, controller?: AbortController): AppThunk =>
  async (dispatch, getState) => {
    const signal = controller?.signal;
    dispatch(loadingChatRoomInfo());

    try {
      const token = getState().auth.token;

      const res = await fetch(`${CHAT_INFO}/${chatId}`, {
        signal,
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!res.ok) throw new Error("Failed to fetch chat room info");

      const data: ChatRoom = await res.json();
      dispatch(loadedChatRoomInfoSuccess(data));
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Chat info fetch aborted");
        return;
      }

      console.error("Chat info error:", e);
      dispatch(loadedChatRoomInfoFailure());
    }
  };

/**
 * Fetch chat room history with AbortController support
 */
export const fetchChatRoomHistory =
  (chatId: string, controller?: AbortController): AppThunk =>
  async (dispatch, getState) => {
    const signal = controller?.signal;
    dispatch(loadingChatRoomHistory());

    try {
      const token = getState().auth.token;

      const res = await fetch(`${CHAT_HISTORY}/${chatId}`, {
        signal,
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!res.ok) throw new Error("Failed to fetch chat history");

      const data: ChatMessage[] = await res.json();
      dispatch(loadedChatRoomHistorySuccess(data));
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Chat history fetch aborted");
        return;
      }

      console.error("Chat history error:", e);
      dispatch(loadedChatRoomHistoryFailure());
    }
  };
