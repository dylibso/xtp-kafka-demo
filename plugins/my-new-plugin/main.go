// Note: run `go doc -all` in this package to see all of the types and functions available.
// ./pdk.gen.go contains the domain types from the host where your plugin will run.
package main

import (
	"fmt"
	"strconv"
	"strings"

	pdk "github.com/extism/go-pdk"
)

var topic string

// This function takes a Record and returns a Record.
// It takes Record as input (A plain key/value record.)
func Transform(input Record) (r []Record, err error) {
	row := input.Value
	if p, err := parse(string(row)); err != nil {
		return nil, err
	} else {
		return append(r, Record{Topic: topic,
			Key: input.Key, Value: []byte(fmt.Sprintf("%s,%f", p.date, p.high))}), nil
	}
}

func init() {
	if name, ok := pdk.GetConfig("topic-name"); ok {
		topic = name
	}
}

type price struct {
	date   string
	open   float64
	high   float64
	low    float64
	close  float64
	volume uint64
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
