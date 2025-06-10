import { FavouriteIcon } from "@/icons/FavouriteIcon";
import { Button } from "./Button";
import { NavLink } from "react-router";
import { Card } from "./Card";
import { cn } from "@/utils";

export interface ProductViewProps extends React.ComponentProps<"div"> {
  product: {
    id: number;
    photo: string;
    title: string;
    description: string;
    price: number;
    favourite: boolean;
  };
  personal?: boolean;
  in_cart?: boolean;
}

export function ProductView({
  product,
  personal = true,
  className,
  ...props
}: ProductViewProps) {
  return (
    <Card
      className={cn("p-4 md:flex-row md:items-center justify-start", className)}
      {...props}
    >
      <NavLink to={`/product/${product.id}`}>
        <img
          src={product.photo}
          alt={product.title}
          className="h-auto w-auto md:max-h-32 rounded-xl"
        />
      </NavLink>
      <div className="text-wrap grow">
        <h4 className="mb-4 font-bold">{product.title}</h4>
        <p className="text-xs md:text-sm">{product.description}</p>
      </div>
      <div className="flex gap-4 items-center">
        {/* Add on click listener add/remove from favourites */}
        {!personal && (
          <>
            <Button variant="secondary">{`${product.price} Р`}</Button>
            <Button size="icon" variant="secondary" className="p-2">
              <FavouriteIcon
                className={`${product.favourite ? "text-destructive" : "text-secondary-foreground"}`}
              />
            </Button>
          </>
        )}
      </div>
    </Card>
  );
}
