document.addEventListener('DOMContentLoaded', () => {
    const createAdButton = document.querySelector(".myAdd_inner__title_btn button");
    const createAdPopUp = document.querySelector(".addAd");

    // Проверка на существование элементов
    if (!createAdButton || !createAdPopUp) {
        console.error("Кнопка или попап не найдены");
        return;
    }

    createAdButton.addEventListener("click", () => {
        // Переключение видимости попапа
        if (createAdPopUp.style.display !== "block") {
            createAdPopUp.style.display = "block";
            createAdPopUp.style.position = "fixed";
        } else {
            createAdPopUp.style.display = "none";
        }
    });
});