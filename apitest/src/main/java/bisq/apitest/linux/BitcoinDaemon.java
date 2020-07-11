/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.apitest.linux;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import static bisq.apitest.linux.BashCommand.isAlive;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static joptsimple.internal.Strings.EMPTY;



import bisq.apitest.config.ApiTestConfig;

// Some cmds:
// bitcoin-cli -regtest generatetoaddress 1 "2MyBq4jbtDF6CfKNrrQdp7qkRc8mKuCpKno"
// bitcoin-cli -regtest getbalance
// note:  getbalance does not include immature coins (<100 blks deep)
// bitcoin-cli -regtest getbalances
// bitcoin-cli -regtest getrpcinfo

@Slf4j
public class BitcoinDaemon extends AbstractLinuxProcess implements LinuxProcess {

    public BitcoinDaemon(ApiTestConfig config) {
        super("bitcoind", config);
    }

    @Override
    public void start() throws InterruptedException, IOException {

        // If the bitcoind binary is dynamically linked to berkeley db libs, export the
        // configured berkeley-db lib path.  If statically linked, the berkeley db lib
        // path will not be exported.
        String berkeleyDbLibPathExport = config.berkeleyDbLibPath.equals(EMPTY) ? EMPTY
                : "export LD_LIBRARY_PATH=" + config.berkeleyDbLibPath + "; ";

        String bitcoindCmd = berkeleyDbLibPathExport
                + config.bitcoinPath + "/bitcoind"
                + " -datadir=" + config.bitcoinDatadir
                + " -daemon";

        BashCommand cmd = new BashCommand(bitcoindCmd).run();
        log.info("Starting ...\n$ {}", cmd.getCommand());

        if (cmd.getExitStatus() != 0)
            throw new IllegalStateException("Error starting bitcoind:\n" + cmd.getError());

        pid = BashCommand.getPid("bitcoind");
        if (!isAlive(pid))
            throw new IllegalStateException("Error starting regtest bitcoind daemon:\n" + cmd.getCommand());

        log.info("Running with pid {}", pid);
        log.info("Log {}", config.bitcoinDatadir + "/regtest/debug.log");
    }

    @Override
    public long getPid() {
        return this.pid;
    }

    @Override
    public void shutdown() throws IOException, InterruptedException {
        try {
            log.info("Shutting down bitcoind daemon...");
            if (!isAlive(pid))
                throw new IllegalStateException("bitcoind already shut down");

            if (new BashCommand("killall bitcoind").run().getExitStatus() != 0)
                throw new IllegalStateException("Could not shut down bitcoind; probably already stopped.");

            MILLISECONDS.sleep(2000); // allow it time to shutdown
            log.info("Stopped");
        } catch (Exception e) {
            throw new IllegalStateException("Error shutting down bitcoind", e);
        } finally {
            if (isAlive(pid))
                //noinspection ThrowFromFinallyBlock
                throw new IllegalStateException("bitcoind shutdown did not work");
        }
    }
}
