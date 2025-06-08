export type LoadingStatus = "idle" | "loading" | "succeeded" | "failed";

export type User = {
  id: number;
  username: string;
  urlPhoto: string;
  dateOfReg: string;
  // For personal profile
  tgId?: number;
  balance?: number;
  subscriptions?: string[];
  role?: string;
};
