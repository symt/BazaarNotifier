// Version 2.0

module.exports = bazaarData => {
  bazaarData.forEach(product => {
    let diff = product.sellOrderPrice * 0.99 - product.buyOrderPrice;
    product.profitFlowPerMinute = ((product.sellCount + product.buyCount) === 0)
        ? 0 : (product.sellCount * product.buyCount) / (10080
        * (product.sellCount + product.buyCount)) * diff;
  });

  bazaarData.sort((a, b) => b.profitFlowPerMinute - a.profitFlowPerMinute);
  return bazaarData;
}

/*

VERSION 1.0

module.exports = bazaarData => {
    bazaarData.sort((a, b) => {
        if (!a.buy || !a.sell) {
            return 1;
        } else if (!b.buy || !b.sell) {
            return -1;
        }

        return (
            b.sell.pricePerUnit * 0.99 -
            b.buy.pricePerUnit -
            (a.sell.pricePerUnit * 0.99 - a.buy.pricePerUnit)
        );
    });
    let i = 1;
    bazaarData.forEach(data => {
        data.rankings = {
            diff: 176 - i++
        };
    });
    i = 1;

    bazaarData.sort((a, b) => {
        if (!a.buy || !a.sell) {
            return 1;
        } else if (!b.buy || !b.sell) {
            return -1;
        }

        return (
            (b.sell.pricePerUnit * 0.99) / b.buy.pricePerUnit -
            (a.sell.pricePerUnit * 0.99) / a.buy.pricePerUnit
        );
    });

    bazaarData.forEach(data => {
        data.rankings.change = 176 - i++;
    });
    i = 1;

    bazaarData.sort((a, b) => {
        return ((b.buyCount + b.sellCount) / 2 - (a.buyCount + a.sellCount) / 2);
    });

    bazaarData.forEach(data => {
        data.rankings.instants = 176 - i++;
    });
    i = 1;

    return bazaarData;
};
*/