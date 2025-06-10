import { FavouriteIcon } from "@/icons/FavouriteIcon";
import { Button } from "./Button";
import { NavLink } from "react-router";
import { Card } from "./Card";
import { cn, photoFromCategory } from "@/utils";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { addToCart, removeFromCart } from "@/features/cart/cartActions";
import type { Product } from "@/types";
import { TrashIcon } from "@/icons/TrashIcon";
import { AddIcon } from "@/icons/AddIcon";
import { CartIcon } from "@/icons/CartIcon";

export interface ProductViewProps extends React.ComponentProps<"div"> {
  product: Product;
  personal?: boolean;
  inCart?: boolean;
}

export function ProductView({
  product,
  personal = true,
  className,
  ...props
}: ProductViewProps) {
  const dispatch = useAppDispatch();
  const cart = useAppSelector((state) => state.cart);
  const { user } = useAppSelector((state) => state.auth);

  return (
    <Card
      className={cn("p-4 md:flex-row md:items-center justify-start", className)}
      {...props}
    >
      <NavLink to={`/product/${product.id}`}>
        <img
          src={photoFromCategory(product.category)}
          alt={product.title}
          className="h-auto w-auto md:max-h-32 rounded-xl"
        />
      </NavLink>
      <div className="text-wrap grow">
        <h4 className="mb-4 font-bold">{product.title}</h4>
        <p className="text-xs md:text-sm">{product.body}</p>
      </div>
      <div className="flex gap-4 items-center">
        {/* Add on click listener add/remove from favourites */}
        <NavLink to={`/product/${product.id}`}>
          <Button variant="secondary">{`${product.price}Р`}</Button>
        </NavLink>
        {user &&
          !personal &&
          (cart.items[product.id] ? (
            <Button
              onClick={() => dispatch(removeFromCart(product))}
              variant="secondary"
            >
              <TrashIcon className="text-secondary-foreground" />
            </Button>
          ) : (
            <Button
              onClick={() => dispatch(addToCart(product))}
              variant="secondary"
            >
              <CartIcon className="text-secondary-foreground" />
            </Button>
          ))}
      </div>
    </Card>
  );
}
