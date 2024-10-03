Chart.defaults.backgroundColor = '#eee';
Chart.defaults.borderColor = '#ccc';
Chart.defaults.color = '#999';

const table = {
  "pricing-data": 0,
  "mavg": 1
}

const config = {
  type: 'line',
  data: {
    datasets: [
      {
        borderColor: '#36A2EB',
        backgroundColor: '#9BD0F5',
        label: 'Closing Price (EURUSD)',
        data: []
      },
      {
        borderColor: '#3600EB',
        backgroundColor: '#9B00F5',
        label: 'Moving Average (EURUSD)',
        data: []
      },

    ]
  },
  options: {
    realtime: {
      duration: 20000,
      refresh: 400,
      delay: 300,
    },
    events: ['click'],
    plugins: {
      tooltip: {
        callbacks: {
          afterBody: function (context) {
            const headers = context[0].raw.headers;
            if (headers != undefined) {
              const l = [];
              for (const property in headers) {
                l.push(`${property}: ${headers[property]}`);
              }
              return l;
            }
            return null;
          }
        }
      }
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
            chart.data.datasets[index].data.push({
              x: v.x,
              y: v.y,
              headers: v.headers,
            });
          }
        }
      }
    }
  }
};

const buf = []


function loadWSSDataAndDisplayCanvas() {

  let streamer = new WebSocket('ws://localhost:8080/viz/me');

  streamer.onmessage = (message) => {
    console.log(message);
    let data = JSON.parse(message.data);

    let timestamp = new Date(data['date']);
    let price = data['price'];

    buf.push({
      type: data.type,
      x: timestamp,
      y: price,
      headers: data.headers,
    });

  }

  config.options.onClick = e => {
    config.options.realtime.pause = !!!config.options.realtime.pause
  };

  const chart = new Chart(
    document.getElementById('chart'),
    config
  );


};