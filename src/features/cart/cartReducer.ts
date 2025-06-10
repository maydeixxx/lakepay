import { type CartAction } from "./cartActions";
import type { Product } from "@/types";

export interface CartState {
  items: {
    [id: number]: Product;
  };
}

const initialState: CartState = localStorage.getItem("cart")
  ? (JSON.parse(localStorage.getItem("cart")!) as CartState)
  : { items: {} };

export default function cartReducer(
  state: CartState = initialState,
  action?: CartAction
): CartState {
  switch (action?.type) {
    case "cart/empty": {
      const newItems = { items: {} };
      localStorage.setItem("cart", JSON.stringify(newItems));
      return newItems;
    }
    case "cart/add": {
      const newItems = {
        items: {
          ...state.items,
          [action.payload!.id]: action.payload!
        }
      };
      localStorage.setItem("cart", JSON.stringify(newItems));
      return newItems;
    }
    case "cart/remove": {
      const {
        items: { [action.payload!.id]: removed, ...rest }
      } = state;
      const newItems = {
        items: {
          ...rest
        }
      };
      localStorage.setItem("cart", JSON.stringify(newItems));
      return newItems;
    }
    default:
      return { ...state };
  }
}
