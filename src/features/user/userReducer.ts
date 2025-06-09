import { USERS_URL } from "@/config";
import {
  failedLoadingUser,
  loadingUser,
  succeededLoadingUser,
  type UserAction
} from "./userActions";
import type { AppThunk } from "@/redux/store";
import type { LoadingStatus, User } from "@/types";

export interface UserState {
  status: LoadingStatus;
  data?: User;
}

const initialState: UserState = {
  status: "idle"
};

export default function userReducer(
  state: UserState = initialState,
  action?: UserAction
) {
  switch (action?.type) {
    case "users/loading": {
      return {
        ...state,
        status: "loading"
      };
    }
    case "users/loadedSuccess": {
      return {
        ...state,
        status: "succeeded",
        data: action!.payload
      };
    }
    case "users/loadedFailure": {
      return {
        ...state,
        status: "failed"
      };
    }
    default:
      return { ...state };
  }
}

export const fetchUser =
  (userId: string): AppThunk =>
  async (dispatch) => {
    dispatch(loadingUser());
    try {
      const response = await fetch(`${USERS_URL}/${userId}`);
      const data: User = await response.json();
      dispatch(succeededLoadingUser(data));
    } catch (e) {
      console.error(e);
      dispatch(failedLoadingUser());
    }
  };
