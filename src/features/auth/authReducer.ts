import type { LoadingStatus, User } from "@/features/types";
import {
  authFailed,
  authLoading,
  authSucceeded,
  type AuthAction
} from "@/features/auth/authActions";
import type { AppThunk } from "@/redux/store";
import { AUTH_URL } from "@/config";

const parseJwt = (token: string): User | undefined => {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    console.error(e);
  }
};

export interface AuthState {
  token?: string;
  user?: User;
  status: LoadingStatus;
}

const initialState: AuthState = {
  status: "idle"
};

export default function AuthReducer(
  state: AuthState = initialState,
  action?: AuthAction
): AuthState {
  switch (action?.type) {
    case "auth/login": {
      console.log(parseJwt(action.payload!));

      return {
        status: "succeeded",
        token: action.payload!,
        user: parseJwt(action.payload!)
      };
    }
    case "auth/loading": {
      return {
        ...state,
        status: "loading"
      };
    }
    case "auth/failed": {
      return {
        status: "failed",
        user: undefined,
        token: undefined
      };
    }
    case "auth/logout": {
      return {
        status: "idle",
        user: undefined,
        token: undefined
      };
    }
    default: {
      return { ...state };
    }
  }
}

type AuthResponse = {
  message: string;
  token: string;
};

// Accepts telegram login data
// TODO: Telegram widget response typing
export const login =
  (telegramData: any): AppThunk =>
  async (dispatch) => {
    dispatch(authLoading());
    try {
      const response = await fetch(AUTH_URL, {
        method: "post",
        body: telegramData
      });
      const data: AuthResponse = await response.json();

      if (data.message == "Logged in") {
        dispatch(authSucceeded(data.token));
      } else {
        dispatch(authFailed());
      }
    } catch (e) {
      console.error(e);
      dispatch(authFailed());
    }
  };
