import { FavouriteIcon } from "@/icons/FavouriteIcon";
import { Button } from "./Button";
import { NavLink } from "react-router";
import { Card } from "./Card";
import { cn, photoFromCategory } from "@/utils";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { addToCart, removeFromCart } from "@/features/cart/cartActions";
import type { Product } from "@/types";
import { TrashIcon } from "@/icons/TrashIcon";
import { CartIcon } from "@/icons/CartIcon";
import XIcon from "@/icons/XIcon";
import { ADS_DELETE } from "@/config";

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
  const { user, token } = useAppSelector((state) => state.auth);

  const deleteProduct = async () => {
    const response = await fetch(`${ADS_DELETE}/${product.id}`, {
      method: "delete",
      body: JSON.stringify({
        userId: user?.id
      }),
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      }
    });
  };

  const ownerControls = () => (
    <Button
      onClick={() => deleteProduct()}
      variant="destructive"
      className="size-12"
    >
      <XIcon className="text-black" />
    </Button>
  );

  const buyerControls = () =>
    cart.items[product.id] ? (
      <Button
        onClick={() => dispatch(removeFromCart(product))}
        variant="secondary"
        className="size-12"
      >
        <TrashIcon className="text-secondary-foreground" />
      </Button>
    ) : (
      <Button
        onClick={() => dispatch(addToCart(product))}
        variant="secondary"
        className="size-12"
      >
        <CartIcon className="text-secondary-foreground" />
      </Button>
    );

  return (
    <Card
      className={cn(
        "p-4 md:flex-row md:items-center justify-start w-full",
        className
      )}
      {...props}
    >
      <NavLink to={`/product/${product.id}`}>
        <img
          src={photoFromCategory(product.category)}
          alt={product.title}
          className="h-auto w-auto md:max-h-32 rounded-xl"
        />
      </NavLink>
      <div className="text-wrap w-full">
        <h4 className="mb-4 font-bold">{product.title}</h4>
        <p className="text-xs md:text-sm">{product.body}</p>
      </div>
      <div className="flex gap-2 items-center">
        {/* Add on click listener add/remove from favourites */}
        <NavLink to={`/product/${product.id}`}>
          <Button variant="secondary">{`${product.price}$`}</Button>
        </NavLink>
        {user && (personal ? ownerControls() : buyerControls())}
      </div>
    </Card>
  );
}
