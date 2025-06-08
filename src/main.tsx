import { createBrowserRouter, RouterProvider } from "react-router";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";

import "./index.css";
import App from "./App.tsx";
import Home from "./pages/index.tsx";
import NotFoundPage from "./pages/not-found.tsx";
import FavouritesPage from "./pages/favourites.tsx";
import { store } from "./redux/store.ts";
import UserPage from "./pages/user.tsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { index: true, Component: Home },
      { path: "/favourite", Component: FavouritesPage },
      { path: "/profile", Component: UserPage },
      { path: "/user/:userId", Component: UserPage },
      { path: "*", Component: NotFoundPage }
    ]
  }
]);

createRoot(document.getElementById("root")!).render(
  <Provider store={store}>
    <StrictMode>
      <RouterProvider router={router} />
    </StrictMode>
  </Provider>
);
