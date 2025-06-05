import { Button } from "@/components/Button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/Popover";
import { Card } from "@/components/Card";
import logo from "@/assets/Lake-Pay-end.png";
import menu from "@/assets/menu.svg";

// TODO: Load categories from server
import game from "@/assets/cs2-category.jpg";
import { Input } from "@/components/Input";
const categories = [
  {
    title: "CS2",
    url: "http://test1.com",
    photo: game
  },
  {
    title: "Fortnite",
    url: "http://test2.com",
    photo: game
  },
  {
    title: "Dota 2",
    url: "http://test3.com",
    photo: game
  },
  {
    title: "Valorant",
    url: "http://test4.com",
    photo: game
  }
];

function HeaderCatalog() {
  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button className="hidden xl:inline-flex">
          <MenuIcon className="text-primary-foreground size-8"></MenuIcon>
          <span>Каталог</span>
        </Button>
      </PopoverTrigger>
      <PopoverContent
        align="start"
        className="bg-primary rounded-3xl border-none grid grid-cols-3 gap-4 w-xl"
      >
        {categories.map((category) => (
          <Card className="bg-background py-0 overflow-hidden">
            <a href={category.url}>
              <img src={category.photo} alt={`Photo of ${category.title}`} />
              <h4 className="text-center py-2 font-bold">{category.title}</h4>
            </a>
          </Card>
        ))}
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
      <ul className="gap-6 hidden lg:flex">
        {destinations.map((dest) => (
          <NavLink to={dest.url} end>
            <Button variant="secondary">
              <img src={dest.icon} alt={dest.title} className="size-8" />
            </Button>
          </NavLink>
        ))}
      </ul>
      <Popover>
        <PopoverTrigger asChild>
          <Button variant="secondary" className="inline-flex lg:hidden">
            <MenuIcon className="text-secondary-foreground size-8"></MenuIcon>
          </Button>
        </PopoverTrigger>
        <PopoverContent className="bg-card border-none">
          <div className="flex flex-col gap-4">
            {destinations.map((dest) => (
              <NavLink to={dest.url} end>
                <Button
                  variant="secondary"
                  className="gap-4 w-full justify-start"
                >
                  <img src={dest.icon} alt={dest.title} className="size-8" />
                  <span>{dest.title}</span>
                </Button>
              </NavLink>
            ))}
          </div>
        </PopoverContent>
      </Popover>
    </nav>
  );
}

export function Header() {
  return (
    <>
      <div className="flex h-32 items-center gap-2 md:gap-8 mx-auto container">
        <NavLink to="/" className="max-w-16 sm:max-w-24 lg:max-w-32">
          <img
            src={logo}
            alt="Lake Pay written in big letters"
            className="w-full h-auto"
          />
        </NavLink>
        <HeaderCatalog />
        <HeaderSearchBar />
        <HeaderNavBar />
      </div>
    </>
  );
}
