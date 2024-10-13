// Note: run `go doc -all` in this package to see all of the types and functions available.
// ./pdk.gen.go contains the domain types from the host where your plugin will run.
package main

import "strings"

// This function takes a Record and returns a Record.
// It takes Record as input (A plain key/value record.)
func Transform(input Record) ([]Record, error) {
	// TODO: fill out your implementation here
	// panic("Function not implemented.")
	topic := input.Topic + "-output"
	value := string(input.Value)
	upper := []byte(strings.ToUpper(value))
	return []Record{{Topic: topic, Value: upper}}, nil
}
