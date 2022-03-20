package main

import (
	"errors"
	"log"
	"os"
	"os/exec"
	"path"
	"path/filepath"
)

var home = os.Getenv("HOME")
var pathprefix = ""

func init() {
	f, err := os.Open("/etc/haveno-envoy.yaml")
	if errors.Is(err, os.ErrNotExist) {
		p, err := os.Executable()
		if err != nil {
			log.Fatal(err)
		}
		pathprefix = filepath.Dir(p)
		if err != nil {
			log.Fatal(err)
		}
	}
	f.Close()
}

func main() {
	log.Println("Running in:", home)
	os.MkdirAll(path.Join(home, ".cache/haveno"), 0750)
	log.Println("Starting monerod")
	go run("haveno-monerod", []string{
		"--stagenet",
		"--no-igd",
		"--hide-my-port",
		"--data-dir", path.Join(home, ".cache/haveno/monero-stagenet"),
		"--add-exclusive-node", "136.244.105.131:38080",
		"--rpc-login", "superuser:abctesting123",
		"--rpc-access-control-origins", "http://localhost:8080",
		"--fixed-difficulty", "100",
		"--non-interactive", // EXTRA: to avoid 'EOF on stdin'
	})
	log.Println("Starting seednode")
	go run("haveno-seednode", []string{
		"--baseCurrencyNetwork=XMR_STAGENET",
		"--useLocalhostForP2P=true",
		"--useDevPrivilegeKeys=true",
		"--nodePort=2002",
		"--appName=haveno-XMR_STAGENET_Seed_2002",
	})
	log.Println("Start alice daemon")
	go run("haveno-daemon", []string{
		"--baseCurrencyNetwork=XMR_STAGENET",
		"--useLocalhostForP2P=true",
		"--useDevPrivilegeKeys=true",
		"--nodePort=5555",
		"--appName=haveno-XMR_STAGENET_Alice",
		"--apiPassword=apitest",
		"--apiPort=9999",
		"--walletRpcBindPort=38091",
		"--passwordRequired=false",
	})
	log.Println("Starting envoy")
	go run("haveno-envoy", []string{"-c", pathprefix + "/etc/haveno-envoy.yaml"})
	// Now we have haevno started (or it is starting.), let's run the frontend.
	run("haveno-frontend", []string{})
}

func run(cmd string, args []string) {
	log.Println(cmd, args)
	c := exec.Command(cmd, args...)
	c.Env = os.Environ()
	c.Env = append(c.Env, "APP_HOME="+pathprefix+"/usr/lib/haveno")
	c.Env = append(c.Env, "JAVA_HOME="+pathprefix+"/usr/lib/haveno/openjdk")
	c.Env = append(c.Env, "MONERO_WALLET_RPC_PATH="+pathprefix+"/usr/lib/haveno")
	c.Stderr = os.Stderr
	c.Stdout = os.Stdout

	err := c.Run()
	if err != nil {
		log.Fatal(cmd, err)
	}
}
