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

const period = 14

type window struct {
	closing [period]float64
	volume  [period]float64
	index   int
}

var (
	w = &window{
		closing: [14]float64{1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 4, 4},
		volume:  [14]float64{1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 4, 4}}
)

// This function takes a Record and returns a Record.
// It takes Record as input (A plain key/value record.)
// And returns Record (A plain key/value record.)
func Transform(input Record) (Record, error) {
	row := input.Value
	if p, err := parse(string(row)); err != nil {
		return Record{}, err
	} else {
		w.append(p.close, float64(p.volume))

		vwap := w.vwap()

		pdk.Log(pdk.LogInfo, fmt.Sprintf("%v", vwap))
		return Record{Topic: "volatility", Key: input.Key, Value: []byte(fmt.Sprintf("%s,%f", p.date, vwap))}, nil
	}
}

func (w *window) vwap() float64 {
	closing, volume := w.closing, w.volume

	var sum float64
	for i := 0; i < period; i++ {
		sum = sum + closing[i]*volume[i]
	}

	var total float64
	for _, x := range volume {
		total = total + x
	}

	return sum / total
}

func (w *window) append(c, v float64) {
	w.index = (w.index + 1) % period
	w.closing[w.index] = c
	w.volume[w.index] = v
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

func (p *price) serialize() string {
	return fmt.Sprintf("%s,%f,%f")
}
