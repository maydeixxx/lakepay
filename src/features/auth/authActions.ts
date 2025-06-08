export type AuthActionType =
  | "auth/login"
  | "auth/failed"
  | "auth/loading"
  | "auth/logout";

export type AuthAction = {
  type: AuthActionType;
  payload?: string;
};

export const authLoading = (): AuthAction => ({
  type: "auth/loading"
});

export const authFailed = (): AuthAction => ({
  type: "auth/failed"
});

export const authSucceeded = (token: string): AuthAction => ({
  type: "auth/login",
  payload: token
});

export const authLogout = (): AuthAction => ({
  type: "auth/logout"
});
