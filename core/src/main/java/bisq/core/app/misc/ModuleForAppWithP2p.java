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

package bisq.core.app.misc;

import bisq.core.alert.AlertModule;
import bisq.core.app.BisqEnvironment;
import bisq.core.app.TorSetup;
import bisq.core.btc.BitcoinModule;
import bisq.core.dao.DaoModule;
import bisq.core.filter.FilterModule;
import bisq.core.network.p2p.seed.DefaultSeedNodeRepository;
import bisq.core.offer.OfferModule;
import bisq.core.proto.network.CoreNetworkProtoResolver;
import bisq.core.proto.persistable.CorePersistenceProtoResolver;
import bisq.core.trade.TradeModule;
import bisq.core.user.Preferences;
import bisq.core.user.User;

import bisq.network.crypto.EncryptionServiceModule;
import bisq.network.p2p.P2PModule;
import bisq.network.p2p.network.BridgeAddressProvider;
import bisq.network.p2p.seed.SeedNodeRepository;

import bisq.common.ClockWatcher;
import bisq.common.app.AppModule;
import bisq.common.config.Config;
import bisq.common.crypto.KeyRing;
import bisq.common.crypto.KeyStorage;
import bisq.common.crypto.PubKeyRing;
import bisq.common.crypto.PubKeyRingProvider;
import bisq.common.proto.network.NetworkProtoResolver;
import bisq.common.proto.persistable.PersistenceProtoResolver;

import org.springframework.core.env.Environment;

import com.google.inject.Singleton;
import com.google.inject.name.Names;

import java.io.File;

import static bisq.common.config.Config.KEY_STORAGE_DIR;
import static bisq.common.config.Config.REFERRAL_ID;
import static bisq.common.config.Config.STORAGE_DIR;
import static bisq.common.config.Config.USE_DEV_MODE;
import static com.google.inject.name.Names.named;

public class ModuleForAppWithP2p extends AppModule {

    public ModuleForAppWithP2p(Environment environment, Config config) {
        super(environment, config);
    }

    @Override
    protected void configure() {
        bind(BisqEnvironment.class).toInstance((BisqEnvironment) environment);
        bind(Config.class).toInstance(config);

        bind(KeyStorage.class).in(Singleton.class);
        bind(KeyRing.class).in(Singleton.class);
        bind(User.class).in(Singleton.class);
        bind(ClockWatcher.class).in(Singleton.class);
        bind(NetworkProtoResolver.class).to(CoreNetworkProtoResolver.class).in(Singleton.class);
        bind(PersistenceProtoResolver.class).to(CorePersistenceProtoResolver.class).in(Singleton.class);
        bind(Preferences.class).in(Singleton.class);
        bind(BridgeAddressProvider.class).to(Preferences.class).in(Singleton.class);
        bind(TorSetup.class).in(Singleton.class);

        bind(SeedNodeRepository.class).to(DefaultSeedNodeRepository.class).in(Singleton.class);

        bind(File.class).annotatedWith(named(STORAGE_DIR)).toInstance(config.getStorageDir());
        bind(File.class).annotatedWith(named(KEY_STORAGE_DIR)).toInstance(config.getKeyStorageDir());

        Boolean useDevPrivilegeKeys = environment.getProperty(Config.USE_DEV_PRIVILEGE_KEYS, Boolean.class, false);
        bind(boolean.class).annotatedWith(Names.named(Config.USE_DEV_PRIVILEGE_KEYS)).toInstance(useDevPrivilegeKeys);

        bind(boolean.class).annotatedWith(named(USE_DEV_MODE)).toInstance(config.isUseDevMode());
        bind(String.class).annotatedWith(named(REFERRAL_ID)).toInstance(config.getReferralId());

        // ordering is used for shut down sequence
        install(new TradeModule(environment, config));
        install(new EncryptionServiceModule(environment, config));
        install(new OfferModule(environment, config));
        install(new P2PModule(environment, config));
        install(new BitcoinModule(environment, config));
        install(new DaoModule(environment, config));
        install(new AlertModule(environment, config));
        install(new FilterModule(environment, config));
        bind(PubKeyRing.class).toProvider(PubKeyRingProvider.class);
    }
}
