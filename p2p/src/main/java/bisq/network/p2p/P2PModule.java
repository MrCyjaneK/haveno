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

package bisq.network.p2p;

import bisq.network.Socks5ProxyProvider;
import bisq.network.p2p.network.Connection;
import bisq.network.p2p.network.NetworkNode;
import bisq.network.p2p.peers.BanList;
import bisq.network.p2p.peers.Broadcaster;
import bisq.network.p2p.peers.PeerManager;
import bisq.network.p2p.peers.getdata.RequestDataManager;
import bisq.network.p2p.peers.keepalive.KeepAliveManager;
import bisq.network.p2p.peers.peerexchange.PeerExchangeManager;
import bisq.network.p2p.storage.P2PDataStorage;
import bisq.network.p2p.storage.persistence.AppendOnlyDataStoreService;
import bisq.network.p2p.storage.persistence.PersistableNetworkPayloadListService;
import bisq.network.p2p.storage.persistence.ProtectedDataStoreService;
import bisq.network.p2p.storage.persistence.ResourceDataStoreService;

import bisq.common.app.AppModule;
import bisq.common.config.Config;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.time.Clock;

import java.io.File;

import java.util.List;

import static bisq.common.config.Config.*;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Providers.of;

public class P2PModule extends AppModule {

    public P2PModule(Config config) {
        super(config);
    }

    @Override
    protected void configure() {
        bind(Clock.class).toInstance(Clock.systemDefaultZone());
        bind(P2PService.class).in(Singleton.class);
        bind(PeerManager.class).in(Singleton.class);
        bind(P2PDataStorage.class).in(Singleton.class);
        bind(AppendOnlyDataStoreService.class).in(Singleton.class);
        bind(ProtectedDataStoreService.class).in(Singleton.class);
        bind(PersistableNetworkPayloadListService.class).in(Singleton.class);
        bind(ResourceDataStoreService.class).in(Singleton.class);
        bind(RequestDataManager.class).in(Singleton.class);
        bind(PeerExchangeManager.class).in(Singleton.class);
        bind(KeepAliveManager.class).in(Singleton.class);
        bind(Broadcaster.class).in(Singleton.class);
        bind(BanList.class).in(Singleton.class);
        bind(NetworkNode.class).toProvider(NetworkNodeProvider.class).in(Singleton.class);
        bind(Socks5ProxyProvider.class).in(Singleton.class);

        requestStaticInjection(Connection.class);

        bindConstant().annotatedWith(Names.named(USE_LOCALHOST_FOR_P2P)).to(config.isUseLocalhostForP2P());

        bind(File.class).annotatedWith(named(TOR_DIR)).toInstance(config.getTorDir());

        bind(int.class).annotatedWith(Names.named(NODE_PORT)).toInstance(config.getNodePort());

        bindConstant().annotatedWith(named(MAX_CONNECTIONS)).to(config.getMaxConnections());

        bind(new TypeLiteral<List<String>>(){}).annotatedWith(named(BAN_LIST)).toInstance(config.getBanList());
        bindConstant().annotatedWith(named(SOCKS_5_PROXY_BTC_ADDRESS)).to(config.getSocks5ProxyBtcAddress());
        bindConstant().annotatedWith(named(SOCKS_5_PROXY_HTTP_ADDRESS)).to(config.getSocks5ProxyHttpAddress());
        bind(File.class).annotatedWith(named(TORRC_FILE)).toProvider(of(config.getTorrcFile())); // allow null value
        bindConstant().annotatedWith(named(TORRC_OPTIONS)).to(config.getTorrcOptions());
        bindConstant().annotatedWith(named(TOR_CONTROL_PORT)).to(config.getTorControlPort());
        bindConstant().annotatedWith(named(TOR_CONTROL_PASSWORD)).to(config.getTorControlPassword());
        bind(File.class).annotatedWith(named(TOR_CONTROL_COOKIE_FILE)).toProvider(of(config.getTorControlCookieFile()));
        bindConstant().annotatedWith(named(TOR_CONTROL_USE_SAFE_COOKIE_AUTH)).to(config.isUseTorControlSafeCookieAuth());
        bindConstant().annotatedWith(named(TOR_STREAM_ISOLATION)).to(config.isTorStreamIsolation());
        bindConstant().annotatedWith(named("MAX_SEQUENCE_NUMBER_MAP_SIZE_BEFORE_PURGE")).to(1000);
    }
}
