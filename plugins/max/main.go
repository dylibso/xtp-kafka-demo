package main

import pdk "github.com/extism/go-pdk"

var (
	max   float64 = 0
	topic string  = "max-output"
)

func init() {
	if name, ok := pdk.GetConfig("topic-name"); ok {
		topic = name
	}
}

func Transform(input Record) ([]Record, error) {
	input.Topic = topic
	if input.Value.Price > max {
		max = input.Value.Price
	}
	input.Value.Price = max
	input.Value.Volume = 0
	return []Record{input}, nil
}
