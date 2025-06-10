import {
  resetChatRoom,
  loadingChatRoomInfo,
  loadedChatRoomInfoSuccess,
  loadedChatRoomInfoFailure,
  loadingChatRoomHistory,
  loadedChatRoomHistorySuccess,
  loadedChatRoomHistoryFailure,
  addChatMessage,
  type ChatRoomAction
} from "./chatRoomActions";
import type { LoadingStatus, ChatRoom, ChatMessage } from "@/types";

export interface ChatRoomState {
  infoStatus: LoadingStatus; // separate status flags keep UI granular
  historyStatus: LoadingStatus;
  info?: ChatRoom;
  history: ChatMessage[];
}

const initialState: ChatRoomState = {
  infoStatus: "idle",
  historyStatus: "idle",
  history: []
};

export default function chatRoomReducer(
  state: ChatRoomState = initialState,
  action?: ChatRoomAction
): ChatRoomState {
  switch (action?.type) {
    case "chatRoom/loadingInfo":
      return { ...state, infoStatus: "loading" };
    case "chatRoom/loadedInfoSuccess":
      return { ...state, infoStatus: "succeeded", info: action.payload };
    case "chatRoom/loadedInfoFailure":
      return { ...state, infoStatus: "failed" };
    case "chatRoom/loadingHistory":
      return { ...state, historyStatus: "loading" };
    case "chatRoom/loadedHistorySuccess":
      return { ...state, historyStatus: "succeeded", history: action.payload };
    case "chatRoom/loadedHistoryFailure":
      return { ...state, historyStatus: "failed" };
    case "chatRoom/addMessage":
      return { ...state, history: [...state.history, action.payload] };
    case "chatRoom/reset":
      return { ...initialState };

    default:
      return state;
  }
}
