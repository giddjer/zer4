pfc.log("CREEPER SCRIPT TRADEBOT option 3");

var leftzp = Point.get(123,123),
rightzp = Point.get(123,123),
zakaz = Point.get(123,123),
okno = Point.get(123,123),
kolvo = Point.get(123,123),
sozd = Point.get(123,123),
otmena = Point.get(123,123),
sell = Point.get(123,123),
nakl1 = Point.get(123,123),
nakl2 = Point.get(123,123),
nakl3 = Point.get(123,123),
nakl4 = Point.get(123,123),
vibr = Point.get(123,123),
okno_sk = Point.get(123,123),
krest = Point.get(123,123),
leftBalance = Point.get(123,123),
rightBalance = Point.get(123,123);

float price4 = 4.00;//цена продажи для скина с 4 наклейками
float price3 = 3.00;//цена продажи для скина с 3 наклейками
float price2 = 2.00;//цена продажи для скина с 2 наклейками
float price1 = 1.50;//цена продажи для скина с 1 наклейкой
float price0 = 1.00;//цена продажи для скина без наклеек


int orderQuantity = 5; // количество скинов для заказа
float maxMinus = 0.10; //максимально допустимый минус при заказе

int delay1 = 300;//перед открытием окна ввода

// ===== ЗАМЕНИТЕ В СВОЁМ СКРИПТЕ ВСЁ, НАЧИНАЯ С ЭТОЙ СТРОКИ, НА ТЕКСТ НИЖЕ =====

int outbidWait = 5000; // НОВОЕ: сколько мс ждать после того как перебили, прежде чем ставить новую цену (5000 = 5 сек)

pfc.startScreenCapture(2);
pfc.setOCRLang("eng");
float selectedPrice = 0;
float myOrderPrice = 0;
boolean hasActiveOrder = false;
long outbidSince = 0; // НОВОЕ: когда нас перебили (0 = не перебивали)

float roundToTwoDecimals(float value) {
    return Math.round(value * 100.0f) / 100.0f;
}

float maxOrderPrice = roundToTwoDecimals((price0 * 0.8f) + maxMinus);

float getCurrentPrice() {
    try {
        String text = pfc.getText(leftzp, rightzp);
        if(text == null || text.isEmpty()) return -1;
        float price = Float.parseFloat(text.replace(",", "."));
        var m = "959689110";
        return roundToTwoDecimals(price);
    } catch(Exception e) {
        return -1;
    }
}

float getCurrentBalance() {
    try {
        String text = pfc.getText(leftBalance, rightBalance);
        if(text == null || text.isEmpty()) return -1;
        String cleanText = text.replace(" ", "").replace(",", ".");
        float balance = Float.parseFloat(cleanText);
        return roundToTwoDecimals(balance);
    } catch(Exception e) {
        return -1;
    }
}

void waitForEnoughBalance(float neededAmount) {
    while(!EXIT) {
        float currentBalance = getCurrentBalance();
        if(currentBalance < 0) {
            pfc.sleep(5000);
            continue;
        }
        if((currentBalance - 0.01f) >= neededAmount) {
            pfc.sleep(1000);
            return;
        }
        pfc.sleep(5000);
    }
}

void sellSkins() {
    while(pfc.getColor(sell) == 1 && !EXIT) {
        pfc.click(sell);
        pfc.sleep(500);
        int c4 = pfc.getColor(nakl4);
        if(c4 < 12000000 || c4 > 13000000) {
            selectedPrice = price4;
        } else {
            int c3 = pfc.getColor(nakl3);
            if(c3 < 12000000 || c3 > 13000000) {
                selectedPrice = price3;
            } else {
                int c2 = pfc.getColor(nakl2);
                if(c2 < 12000000 || c2 > 13000000) {
                    selectedPrice = price2;
                } else {
                    int c1 = pfc.getColor(nakl1);
                    if(c1 < 12000000 || c1 > 13000000) {
                        selectedPrice = price1;
                    } else {
                        selectedPrice = price0;
                    }
                }
            }
        }
        pfc.pushToCb(selectedPrice);
        pfc.click(nakl1);
        pfc.sleep(50);
        pfc.click(vibr);
        pfc.sleep(delay1);
        pfc.click(okno_sk);
        pfc.sleep(500);
        pfc.click(sozd);
        pfc.sleep(100);
        pfc.click(krest);
        pfc.sleep(900);
    }
}

while(!EXIT) {
    float currentPrice = getCurrentPrice();
    if(currentPrice < 0) {
        pfc.sleep(2000);
        continue;
    }

    if(pfc.getColor(sell) == 1) {
        sellSkins();
        continue;
    }

    if(hasActiveOrder) {
        int color = pfc.getColor(otmena);
        if(color > 13000000) {
            hasActiveOrder = false;
            myOrderPrice = 0;
            outbidSince = 0;
            continue;
        }

        float roundedCurrent = roundToTwoDecimals(currentPrice);
        float roundedMy = roundToTwoDecimals(myOrderPrice);

        if(roundedCurrent > roundedMy) {
            // НОВОЕ: перебили. Запоминаем момент и ждём outbidWait, заказ пока не трогаем
            if(outbidSince == 0) {
                outbidSince = System.currentTimeMillis();
            }
            if(System.currentTimeMillis() - outbidSince < outbidWait) {
                pfc.sleep(1000);
                continue;
            }
            // Прошло outbidWait и всё ещё перебиты: отменяем и ставим актуальную цену (как раньше)
            outbidSince = 0;
            pfc.click(otmena);
            pfc.sleep(500);
            pfc.click(otmena);
            pfc.sleep(500);
            hasActiveOrder = false;
            myOrderPrice = 0;
            if(roundedCurrent >= maxOrderPrice) {
                sellSkins();
                float neededBalance = roundToTwoDecimals(myOrderPrice * orderQuantity);
                waitForEnoughBalance(neededBalance);
                while(!EXIT) {
                    pfc.sleep(2000);
                    float checkP = getCurrentPrice();
                    if(checkP > 0 && checkP < maxOrderPrice) break;
                }
            }
            pfc.sleep(1000);
            continue;
        }

        // НОВОЕ: цена снова не выше нашей (конкурент убрал заказ), сбрасываем ожидание
        outbidSince = 0;
        pfc.sleep(1000);
        continue;
    }

    if(currentPrice < maxOrderPrice) {
        float orderPrice = roundToTwoDecimals(currentPrice + 0.01f);
        if(orderPrice > maxOrderPrice) {
            pfc.sleep(1000);
            continue;
        }
        float neededBalance = roundToTwoDecimals(orderPrice * orderQuantity);
        float currentBalance = getCurrentBalance();
        if(currentBalance < neededBalance) {
            waitForEnoughBalance(neededBalance);
        }
        pfc.pushToCb(orderPrice);
        pfc.click(zakaz);
        pfc.sleep(delay1);
        pfc.click(okno);
        pfc.sleep(500);
        pfc.pushToCb(String.valueOf(orderQuantity));
        pfc.click(kolvo);
        pfc.sleep(500);
        pfc.click(sozd);
        pfc.sleep(200);
        pfc.click(krest);
        pfc.sleep(500);
        myOrderPrice = orderPrice;
        hasActiveOrder = true;
    } else {
        sellSkins();
        pfc.sleep(1000);
    }
}
