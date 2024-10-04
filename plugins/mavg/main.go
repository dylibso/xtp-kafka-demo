// Note: run `go doc -all` in this package to see all of the types and functions available.
// ./pdk.gen.go contains the domain types from the host where your plugin will run.
package main

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/extism/go-pdk"
)

type price struct {
	date   string
	open   float64
	high   float64
	low    float64
	close  float64
	volume uint64
}

type Volatility struct {
	Value float64
}

const period = 10

type window struct {
	closing [period]float64
	index   uint
	denom   uint
}

var (
	w     = &window{closing: [period]float64{}, index: 0, denom: 0}
	topic = "mavg"
)

func init() {
	if name, ok := pdk.GetConfig("topic-name"); ok {
		topic = name
	}
}

// This function takes a Record and returns a Record.
// It takes Record as input (A plain key/value record.)
// And returns Record (A plain key/value record.)
func Transform(input Record) (r []Record, err error) {
	row := input.Value
	if p, err := parse(string(row)); err != nil {
		return nil, err
	} else {
		w.append(p.close, float64(p.volume))

		mavg := w.mavg()

		pdk.Log(pdk.LogInfo, fmt.Sprintf("%v", mavg))
		return append(r, Record{Topic: topic, Key: input.Key, Value: []byte(fmt.Sprintf("%s,%f", p.date, mavg))}), nil
	}
}

func (w *window) mavg() float64 {
	var sum float64
	for _, v := range w.closing {
		sum += v
	}
	return sum / float64(w.denom)
}

func (w *window) append(c, v float64) {
	w.index = (w.index + 1) % period
	w.closing[w.index] = c
	if w.denom < period {
		w.denom += 1
	}
}

func parse(in string) (p price, err error) {
	s := strings.Split(in, ",")
	p.date = s[0]
	if p.open, err = strconv.ParseFloat(s[1], 64); err != nil {
		return
	}
	if p.high, err = strconv.ParseFloat(s[2], 64); err != nil {
		return
	}
	if p.low, err = strconv.ParseFloat(s[3], 64); err != nil {
		return
	}
	if p.close, err = strconv.ParseFloat(s[4], 64); err != nil {
		return
	}
	p.volume, err = strconv.ParseUint(s[5], 10, 64)
	return
}
