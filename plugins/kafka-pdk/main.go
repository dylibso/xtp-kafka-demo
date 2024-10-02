// Note: run `go doc -all` in this package to see all of the types and functions available.
// ./pdk.gen.go contains the domain types from the host where your plugin will run.
package main

import "github.com/extism/go-pdk"

var topic = "output-topic"

func init() {
	if t, ok := pdk.GetConfig("output-topic"); ok {
		topic = t
	}
}

// This function takes a Record and returns a Record.
// It takes Record as input (A plain key/value record.)
// And returns Record (A plain key/value record.)
func Transform(input Record) (Record, error) {
	input.Topic = topic
	return input, nil
}
