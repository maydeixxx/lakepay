import type { Product } from "@/types";

export type CartActionTypes = "cart/empty" | "cart/add" | "cart/remove";

export type CartAction = {
  type: CartActionTypes;
  payload?: Product;
};

export const emptyCart = (): CartAction => {
  return {
    type: "cart/empty"
  };
};

export const addToCart = (product: Product): CartAction => {
  return {
    type: "cart/add",
    payload: product
  };
};

export const removeFromCart = (product: Product): CartAction => {
  return {
    type: "cart/remove",
    payload: product
  };
};
