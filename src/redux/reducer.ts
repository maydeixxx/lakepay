import { combineReducers } from "redux";
import userReducer from "../features/user/userReducer";
import authReducer from "@/features/auth/authReducer";
import cartReducer from "@/features/cart/cartReducer";

const rootReducer = combineReducers({
  users: userReducer,
  auth: authReducer,
  cart: cartReducer
});

export default rootReducer;
