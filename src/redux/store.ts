import { applyMiddleware, legacy_createStore as createStore } from "redux";
import rootReducer, { type RootState } from "@/redux/reducer.ts";
import { useDispatch, useSelector, useStore } from "react-redux";
import { thunk, type ThunkAction } from "redux-thunk";
import type { UserAction } from "@/features/user/userActions";

const middleware = applyMiddleware(thunk);
export const store = createStore(rootReducer, middleware);

export type AppStore = typeof store;
export type AppDispatch = AppStore["dispatch"];
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  UserAction
>;

export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
export const useAppStore = useStore.withTypes<AppStore>();
