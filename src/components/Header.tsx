import { Button, type ButtonProps } from "@/components/Button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/Popover";
import { Card } from "@/components/Card";
import logo from "@/assets/Lake-Pay-end.png";
import { Input } from "@/components/Input";

function HeaderCatalog({
  align,
  ...props
}: ButtonProps & { align?: "start" | "end" | "center" }) {
  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button {...props}>
          <MenuIcon className="size-8"></MenuIcon>
          <span>Каталог</span>
        </Button>
      </PopoverTrigger>
      <PopoverContent
        align={align}
        className="bg-primary rounded-3xl border-none max-w-xl w-screen"
      >
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 overflow-y-auto max-h-96 md:max-h-64 pr-2">
          {categories.map((category, index) => (
            <Card className="bg-background py-0 overflow-hidden" key={index}>
              <NavLink to={`/category/${category.name}`}>
                <img src={category.photo} alt={`Photo of ${category.title}`} />
                <h4 className="text-center py-2 font-bold">{category.title}</h4>
              </NavLink>
            </Card>
          ))}
        </div>
      </PopoverContent>
    </Popover>
  );
}

function HeaderSearchBar() {
  // TODO: Implement search logic
  return (
    <Input
      placeholder="Поиск..."
      className="px-4 py-6 border-none bg-background shrink min-w-48"
    />
  );
}

// TODO: Load nav destinations from server
import cartIcon from "@/assets/cart.svg";
import favouriteIcon from "@/assets/favorite.svg";
import chatIcon from "@/assets/chat.svg";
import userIcon from "@/assets/user.svg";
import { NavLink } from "react-router";
import { MenuIcon } from "@/icons/MenuIcon";
import { categories } from "@/config";

const destinations = [
  {
    title: "Cart",
    url: "/cart",
    icon: cartIcon
  },
  {
    title: "Favourite",
    url: "/favourite",
    icon: favouriteIcon
  },
  {
    title: "Chat",
    url: "/chat",
    icon: chatIcon
  },
  {
    title: "Profile",
    url: "/profile",
    icon: userIcon
  }
];

function HeaderNavBar() {
  return (
    <nav className="shrink-0">
      <ul className="gap-6 hidden xl:flex">
        {destinations.map((dest) => (
          <NavLink to={dest.url} key={JSON.stringify(dest)} end>
            <Button variant="secondary">
              <img src={dest.icon} alt={dest.title} className="size-8" />
            </Button>
          </NavLink>
        ))}
      </ul>
      <Popover>
        <PopoverTrigger asChild>
          <Button variant="secondary" className="inline-flex xl:hidden">
            <MenuIcon className="text-secondary-foreground size-8"></MenuIcon>
          </Button>
        </PopoverTrigger>
        <PopoverContent className="bg-card border-none">
          <div className="flex flex-col gap-4">
            {destinations.map((dest) => (
              <NavLink to={dest.url} key={JSON.stringify(dest)} end>
                <Button
                  variant="secondary"
                  className="gap-4 w-full justify-start"
                >
                  <img src={dest.icon} alt={dest.title} className="size-8" />
                  <span>{dest.title}</span>
                </Button>
              </NavLink>
            ))}
            <HeaderCatalog
              variant="secondary"
              align="end"
              className="justify-start px-6"
            />
          </div>
        </PopoverContent>
      </Popover>
    </nav>
  );
}

export function Header() {
  return (
    <>
      <div className="sticky top-0 bg-gradient-top border-b-2 border-gradient-bottom">
        <div className="flex h-32 items-center gap-2 md:gap-8 mx-auto container ">
          <NavLink to="/" className="max-w-16 sm:max-w-24 lg:max-w-32">
            <img
              src={logo}
              alt="Lake Pay written in big letters"
              className="w-full h-auto"
            />
          </NavLink>
          <HeaderCatalog className="hidden xl:inline-flex" align="start" />
          <HeaderSearchBar />
          <HeaderNavBar />
        </div>
      </div>
    </>
  );
}
