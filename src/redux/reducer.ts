import { combineReducers } from "redux";
import userReducer from "../features/user/userReducer";
import authReducer from "@/features/auth/authReducer";
import cartReducer from "@/features/cart/cartReducer";
import chatRoomReducer from "@/features/chat/chatRoomReducer";

const rootReducer = combineReducers({
  users: userReducer,
  auth: authReducer,
  cart: cartReducer,
  chat: chatRoomReducer
});

export default rootReducer;
