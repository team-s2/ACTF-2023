const seedrandom = require('seedrandom')

function genPayload(seed, randoms) {
    let rng = seedrandom.alea(seed, {
        state: true
    });

    let traces = []

    let rngWrap = () => {
        let rnd = rng();
        randoms.push(rnd);
        return rnd;
    }

    let flagcnt = 0;

    while (true) {
        while (true)
        {
            let rnd = rngWrap();
            if (rnd < 0.6) {
                traces.push([
                    'event', ['HourBeginTasks', []]
                ])
                traces.push([
                    'action', ['DisplayChoices', [1]]
                ])
                traces.push([
                    'action', ['CoinFlip', [rnd]]
                ])
                traces.push([
                    'action', ['DisplayRandomMessage', [rngWrap(), 0]]
                ])
                traces.push([
                    'action', ['CoinFlip', [rngWrap()]]
                ])
                break;
            }
            else {
                traces.push([
                    'event', ['haha', []]
                ])
                traces.push([
                    'action', ['DisplayRandomMessage', [rnd, 0]]
                ])
            }
        }

        while (true)
        {
            let rnd = rngWrap();
            if (rnd < 0.8) {
                traces.push([
                    'event', ['HourBeginTasks', []]
                ])
                traces.push([
                    'action', ['DisplayChoices', [2]]
                ])
                traces.push([
                    'action', ['CoinFlip', [rnd]]
                ])
                break;
            }
            else {
                traces.push([
                    'event', ['haha', []]
                ])
                traces.push([
                    'action', ['DisplayRandomMessage', [rnd, 0]]
                ])
            }
        }

        while (true)
        {
            let rnd = rngWrap();
            if (rnd < 0.7) {
                traces.push([
                    'event', ['HourBeginTasks', []]
                ])
                traces.push([
                    'action', ['DisplayChoices', [3]]
                ])
                traces.push([
                    'action', ['CoinFlip', [rnd]]
                ])
                break;
            }
            else {
                traces.push([
                    'event', ['haha', []]
                ])
                traces.push([
                    'action', ['DisplayRandomMessage', [rnd, 0]]
                ])
            }
        }

        while (true)
        {
            let rnd = rngWrap();
            if (rnd < 0.7) {
                traces.push([
                    'event', ['HourBeginTasks', []]
                ])
                traces.push([
                    'action', ['DisplayChoices', [4]]
                ])
                traces.push([
                    'action', ['CoinFlip', [rnd]]
                ])
                break;
            }
            else {
                traces.push([
                    'event', ['haha', []]
                ])
                traces.push([
                    'action', ['DisplayRandomMessage', [rnd, 0]]
                ])
            }
        }

        while (true)
        {
            let rnd = rngWrap();
            if (rnd < 0.6) {
                traces.push([
                    'event', ['flagCheck', []]
                ])
                traces.push([
                    'action', ['CoinFlip', [rnd]]
                ])
                traces.push([
                    'action', ['CoinFlip', [rngWrap()]]
                ])
                break;
            }
            else {
                traces.push([
                    'event', ['haha', []]
                ])
                traces.push([
                    'action', ['DisplayRandomMessage', [rnd, 0]]
                ])
            }
        }

        flagcnt += 1;

        if(flagcnt >= 8)
            break;
    }

    return randoms, traces
}

let seed = "5702857508689732"
let randoms = []
let traces = genPayload(seed, randoms)

console.log(randoms)
console.log(traces)

let json = JSON.stringify({
    "randomseed": seed,
    "randoms": randoms,
    'traces': traces,
})

fetch("http://120.46.65.156:23000/api/verify", {
    method: 'POST',
    body: json,
    headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    },
}).then(data => {
    data.text().then(a => {
        console.log(a);
    });
})
