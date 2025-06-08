import type { User } from "@/types";

export type UserActionType =
  | "users/reset"
  | "users/loading"
  | "users/loadedSuccess"
  | "users/loadedFailure";

export type UserAction = {
  type: UserActionType;
  payload?: User;
};

export const resetUser = (): UserAction => {
  return {
    type: "users/reset"
  };
};

export const loadingUser = (): UserAction => {
  return {
    type: "users/loading"
  };
};

export const succeededLoadingUser = (user: User): UserAction => {
  return {
    type: "users/loadedSuccess",
    payload: user
  };
};

export const failedLoadingUser = (): UserAction => {
  return {
    type: "users/loadedFailure"
  };
};
