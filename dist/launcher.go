package main

import (
	"log"
	"net/http"
	"os"
	"os/exec"
	"path"
	"strconv"

	"github.com/haveno-dex/haveno/dist/ui"
	"github.com/zserge/lorca"
)

var home = os.Getenv("HOME")

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
	go run("haveno-envoy", []string{"-c", "/etc/haveno-envoy.yaml"})
	// Now we have haevno started (or it is starting.), let's run the frontend.
	go listenWeb(43313)
	ui, err := lorca.New("http://127.0.0.1:43313", "", 480, 320)
	log.Println(err)
	defer ui.Close()
	<-ui.Done()
}

func listenWeb(port int) {
	http.Handle("/", http.FileServer(http.FS(ui.Files)))
	err := http.ListenAndServe(":"+strconv.Itoa(port), nil)
	if err != nil {
		log.Fatal("listenWeb", err)
	}
}

func run(cmd string, args []string) {
	log.Println(cmd, args)
	c := exec.Command(cmd, args...)
	c.Env = os.Environ()
	c.Env = append(c.Env, "APP_HOME=/usr/lib/haveno")
	c.Stderr = os.Stderr
	c.Stdout = os.Stdout

	err := c.Run()
	if err != nil {
		log.Fatal(cmd, err)
	}
}
