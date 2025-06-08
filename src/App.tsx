import "./App.css";
import { Outlet } from "react-router";
import { Header } from "./components/Header";

function App() {
  return (
    <>
      <Header />
      <Outlet></Outlet>
    </>
  );
}

export default App;
