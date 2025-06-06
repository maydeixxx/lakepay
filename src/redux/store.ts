import { legacy_createStore as createStore } from "redux";
import rootReducer, { type RootState } from "@/redux/reducer.ts";
import { useDispatch, useSelector, useStore } from "react-redux";

export const store = createStore(rootReducer);

export type AppStore = typeof store;
export type AppDispatch = AppStore["dispatch"];

export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
export const useAppStore = useStore.withTypes<AppStore>();
