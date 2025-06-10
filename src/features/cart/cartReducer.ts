import { type CartAction } from "./cartActions";
import type { Product } from "@/types";

export interface CartState {
  items: Product[];
}

const initialState: CartState = {
  items: []
};

export default function cartReducer(
  state: CartState = initialState,
  action?: CartAction
) {
  switch (action?.type) {
    case "cart/empty": {
      return { items: [] };
    }
    case "cart/add": {
      return {
        items: [...state.items, action.payload]
      };
    }
    case "cart/remove": {
      return {
        items: state.items.filter((item) => item.id != action.payload?.id)
      };
    }
    default:
      return { ...state };
  }
}
