{:test-apps [
             {:package-name "com.android.helloworldjava"
              :start-activity "HelloWorld"
              :main-activity "HelloWorld"}
             {:package-name "com.android.helloworldlein"
              :start-activity "SplashActivity"
              :main-activity "HelloWorld"}
             {:package-name "com.android.helloworldminimal"
              :start-activity "HelloWorld"
              :main-activity "HelloWorld"}
             ]
 :output-dir "/home/ennus/school/thesis/experiments/benchmarks/logs"
 :device-props ["dalvik.vm.dexopt-flags"
                "dalvik.vm.heapgrowthlimit"
                "dalvik.vm.heapmaxfree"
                "dalvik.vm.heapminfree"
                "dalvik.vm.heapsize"
                "dalvik.vm.heapstartsize"
                "dalvik.vm.heaptargetutilization"
                "dalvik.vm.stack-trace-file"
                "persist.sys.dalvik.vm.lib"
                "ro.board.platform"
                "ro.boot.baseband"
                "ro.boot.bootloader"
                "ro.boot.emmc"
                "ro.boot.hardware.ddr"
                "ro.boot.hardware.display"
                "ro.boot.hardware.sku"
                "ro.boot.hardware"
                "ro.boot.serialno"
                "ro.bootloader"
                "ro.bootmode"
                "ro.build.characteristics"
                "ro.build.date.utc"
                "ro.build.date"
                "ro.build.description"
                "ro.build.display.id"
                "ro.build.fingerprint"
                "ro.build.host"
                "ro.build.id"
                "ro.build.product"
                "ro.build.tags"
                "ro.build.type"
                "ro.build.user"
                "ro.build.version.codename"
                "ro.build.version.incremental"
                "ro.build.version.release"
                "ro.build.version.sdk"
                "ro.product.board"
                "ro.product.brand"
                "ro.product.cpu.abi2"
                "ro.product.cpu.abi"
                "ro.product.device"
                "ro.product.locale.language"
                "ro.product.locale.region"
                "ro.product.manufacturer"
                "ro.product.model"
                "ro.product.name"
                ]} 
