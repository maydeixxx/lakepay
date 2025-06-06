import { combineReducers } from "redux";
import userReducer, { type UserState } from "../features/user/userReducer";

export interface RootState {
  users: UserState;
}

const rootReducer = combineReducers<RootState>({
  users: userReducer()
});

export default rootReducer;
