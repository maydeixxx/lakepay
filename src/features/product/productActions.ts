import type { Product } from "@/types";

export type ProductActionType =
  | "product/reset"
  | "product/loading"
  | "product/loadedSuccess"
  | "product/loadedFailure";

export type ProductAction = {
  type: ProductActionType;
  payload?: Product;
};

export const resetUser = (): ProductAction => {
  return {
    type: "product/reset"
  };
};

export const loadingUser = (): ProductAction => {
  return {
    type: "product/loading"
  };
};

export const succeededLoadingUser = (product: Product): ProductAction => {
  return {
    type: "product/loadedSuccess",
    payload: product
  };
};

export const failedLoadingUser = (): ProductAction => {
  return {
    type: "product/loadedFailure"
  };
};
