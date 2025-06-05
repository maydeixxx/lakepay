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
      <PopoverTrigger>
        <Button icon={menu}>Каталог</Button>
      </PopoverTrigger>
      <PopoverContent
        align="start"
        className="bg-primary rounded-3xl border-none grid grid-cols-3 gap-4 w-xl"
      >
        {categories.map((category) => (
          <Card className="bg-background py-0 overflow-hidden">
            <a href={category.url}>
              <img src={category.photo} alt={`Photo of ${category.title}`} />
              <h4 className="text-center py-2">{category.title}</h4>
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
      className="px-4 py-6 border-none bg-background shrink"
    />
  );
}

// TODO: Load nav destinations from server
import cartIcon from "@/assets/cart.svg";
import favouriteIcon from "@/assets/favorite.svg";
import chatIcon from "@/assets/chat.svg";
import userIcon from "@/assets/user.svg";
import { NavLink } from "react-router";

const destinations = [
  {
    url: "/cart",
    icon: cartIcon
  },
  {
    url: "/favourite",
    icon: favouriteIcon
  },
  {
    url: "/chat",
    icon: chatIcon
  },
  {
    url: "/profile",
    icon: userIcon
  }
];

function HeaderNavBar() {
  return (
    <nav className="shrink-0">
      <ul className="flex gap-6">
        {destinations.map((dest) => (
          <NavLink to={dest.url} end>
            <Button icon={dest.icon} variant="secondary" />
          </NavLink>
        ))}
      </ul>
    </nav>
  );
}

export function Header() {
  return (
    <>
      <div className="flex h-32 items-center gap-8 mx-auto container">
        <img
          src={logo}
          alt="Lake Pay written in big letters"
          className="max-h-24 mr-16"
        />
        <HeaderCatalog />
        <HeaderSearchBar />
        <HeaderNavBar />
      </div>
    </>
  );
}
