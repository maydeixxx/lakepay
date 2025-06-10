import { applyMiddleware, legacy_createStore as createStore } from "redux";
import rootReducer from "@/redux/reducer.ts";
import { useDispatch, useSelector, useStore } from "react-redux";
import { thunk, type ThunkAction } from "redux-thunk";
import type { UserAction } from "@/features/user/userActions";
import type { AuthAction } from "@/features/auth/authActions";
import type { CartAction } from "@/features/cart/cartActions";
import type { ChatRoomAction } from "@/features/chat/chatRoomActions";

const middleware = applyMiddleware(thunk);
export const store = createStore(rootReducer, {}, middleware);

export type RootState = ReturnType<typeof store.getState>;
export type AppStore = typeof store;
export type AppDispatch = AppStore["dispatch"];
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  UserAction | AuthAction | CartAction | ChatRoomAction
>;

export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
export const useAppStore = useStore.withTypes<AppStore>();
