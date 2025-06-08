import { combineReducers } from "redux";
import userReducer from "../features/user/userReducer";
import authReducer from "@/features/auth/authReducer";

const rootReducer = combineReducers({
  users: userReducer,
  auth: authReducer
});

export default rootReducer;
