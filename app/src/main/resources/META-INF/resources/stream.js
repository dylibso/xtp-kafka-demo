Chart.defaults.backgroundColor = '#eee';
Chart.defaults.borderColor = '#ccc';
Chart.defaults.color = '#999';

var randomColor = (function () {
  var randomInt = function (min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  };
  return function () {
    var h = randomInt(0, 360);
    var s = randomInt(42, 98);
    var l = randomInt(38, 90);
    return "hsl(" + h + "," + s + "%," + l + "%)";
  };
})();

palette = [
  '#FF5EC7',
  '#614FC9',
  '#82FF54',
  '#E9FF57',
  '#52FF8B',
  '#FF5C64',
  '#4FFFF3',
  '#FFB259',
  '#4DA0FF',
  '#D560FC',
]

const table = {}

const datasets = [];

const config = {
  type: 'line',
  data: {
    datasets: datasets
  },
  options: {
    realtime: {
      duration: 20000,
      refresh: 200,
      delay: 300,
    },
    elements: {
      point: {
        radius : customRadius,
        display: true
      }
    },
    plugins: {
      tooltip: {
        position: 'nearest',
        callbacks: {
          afterBody: function (context) {
            const headers = context[0].raw.headers;
            const l = [];

            if (headers != undefined) {
              for (const property in headers) {
                l.push(`${property}: ${headers[property]}`);
              }
            }
            return l;
          }
        }
      },
    },

    scales: {
      x: {
        type: 'realtime',
        realtime: {
          onRefresh: chart => {
            const v = buf.pop()
            if (v == undefined || table[v.type] == undefined) {
              return;
            }
            const index = table[v.type]
            chart.data.datasets[index].data.push(v);
          }
        }
      }
    }
  }
};

const buf = []
const lastTime = {}
const lastValue = {}

function customRadius(ctx) {
  if (ctx?.raw?.highlight) {
    return 10;
  } else {
    return 4
  }

}

function loadWSSDataAndDisplayCanvas() {

  let streamer = new WebSocket('ws://localhost:8080/viz/me');
  config.options.onClick = e => {
    config.options.realtime.pause = !!!config.options.realtime.pause
  };

  const chart = new Chart(
    document.getElementById('chart'),
    config
  );

  streamer.onmessage = (message) => {
    let data = JSON.parse(message.data);

    let timestamp = new Date(data.value.date);
    let price = data.value.price;


    if (table[data.topic] === undefined) {
      const idx = Object.keys(table).length;
      table[data.topic] = idx;
      const col = palette[ idx % palette.length ];
      datasets.push({
        borderColor: col,
        backgroundColor: col,
        label: data.topic,
        data: [],
      })
      chart.update();
    }


    const lastV = lastValue[data.topic];
    const lastPluginName = lastV?.headers?.['plugin-name'];
    const lastPluginUpdate = lastV?.headers?.['plugin-timestamp'];

    const headers = {}
    for (d of data.headers) {
      headers[d.key] = d.value
    }

    const v = {
      type: data.topic,
      x: timestamp,
      y: price,
      headers: headers,
      highlight: (lastPluginName !== undefined && headers?.['plugin-name'] != lastPluginName) ||
                 (lastPluginUpdate !== undefined && headers?.['plugin-timestamp'] != lastPluginUpdate)
    };
    lastValue[data.topic] = v;
    buf.push(v);

  }
};