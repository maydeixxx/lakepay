import { combineReducers } from "redux";
import userReducer from "../features/user/userReducer";

const rootReducer = combineReducers({
  users: userReducer
});

export default rootReducer;
