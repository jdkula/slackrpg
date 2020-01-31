function parseDice(roll) {
    let js = JSON.parse(roll);
    let rolls = [];
    let total_sum = 0;
    for (let rollResult of js) {
        let results = [];
        let total = rollResult.modifier;
        for(let result of rollResult.results) {
            let cls = "roll_result";
            let rln = result.roll;
            let xtra = "";
            if(result.dropped) {
                cls += " dropped";
                xtra = `<span class="accessible_only">drop </span>`;
            }
            results.push(`<span class="${cls}">${xtra}${rln}</span>`);
            if(result.dropped !== true) {
                total += result.roll;
            }
        }
        let modtext = "";
        if(rollResult.modifier !== 0) {
            modtext = ` + <span title="modifier">${rollResult.modifier}</span>`;
        }
        rolls.push(`<span title="dice definition" class="dice_definition">${rollResult.numDice}d${rollResult.numSides}</span>: <span title="roll total" class="total">${total}</span>: [<span title="dice breakdown" class="breakdown">${results.join(" + ")}</span>]${modtext}`);
        total_sum += total;
    }
    let ttltxt = '';
    if(rolls.length !== 1) {
        ttltxt = `Total: ${total_sum}<br>`
    }
    return ttltxt + rolls.join("<br>")

}

$(document).ready(function () {
    const output = $("#output");
    const roll = $("#roll");
    const form = $("#diceroller");

    form.submit(function (e) {
        e.preventDefault();
        let xhr = new XMLHttpRequest();
        xhr.addEventListener('load', function () {
            let txt = xhr.responseText;
            let parsed;
            try {
                parsed = parseDice(txt);
            } catch (e) {
                console.error(e);
                parsed = txt
            }
            output.html(parsed).show();
        });
        xhr.addEventListener('error', function () {
            let txt = xhr.responseText;
            output.text(txt).show();
        });
        let url = `${window.location.protocol}//${window.location.host}/dice`;
        xhr.open("POST", url);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        let urlEncodedData = encodeURIComponent("roll") + "=" + encodeURIComponent(roll.val());
        urlEncodedData = urlEncodedData.replace(/%20/g, '+');
        xhr.send(urlEncodedData);
        return false;
    });
});