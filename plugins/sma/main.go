// Note: run `go doc -all` in this package to see all of the types and functions available.
// ./pdk.gen.go contains the domain types from the host where your plugin will run.
package main

import (
	"github.com/cinar/indicator"
	"github.com/extism/go-pdk"
)

const period = 10

type window struct {
	prices       [period]float64
	index, count uint
}

var (
	w     = &window{prices: [period]float64{}}
	topic = "sma-output"
)

// append adds the Price of an Order to the current index.
func (w *window) append(o Order) {
	w.count++
	w.index = w.count % period
	w.index = (w.index + 1) % period
	w.prices[w.index] = o.Price
}

// ready is true when the window has been filled.
func (w *window) ready() bool {
	return w.count >= period
}

// sma applies the SMA function and returns the result.
func (w *window) sma() float64 {
	return indicator.Sma(period, w.prices[:])[period-1]
}

func init() {
	if name, ok := pdk.GetConfig("topic-name"); ok {
		topic = name
	}
}

func Transform(input Record) ([]Record, error) {
	w.append(input.Value)
	if w.ready() {
		input.Topic = topic
		input.Value.Price = w.sma()
		input.Value.Volume = 0
		return []Record{input}, nil
	} else {
		return []Record{}, nil
	}
}
